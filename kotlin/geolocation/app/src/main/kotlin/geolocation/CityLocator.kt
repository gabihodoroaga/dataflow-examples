package geolocation

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.api.services.bigquery.model.TableRow
import java.io.IOException
import kotlin.text.Charsets
import org.geotools.data.DataStoreFinder
import org.geotools.data.collection.SpatialIndexFeatureCollection
import org.geotools.factory.CommonFactoryFinder
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.GeodeticCalculator
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.opengis.feature.simple.SimpleFeature

class CityLocator {

    class City(
            val name: String,
            val country: String,
            val state: String,
            val lat: Double,
            val lon: Double
    ) {}

    companion object {

        val filterFactory = CommonFactoryFinder.getFilterFactory2()
        val geometryFactory = GeometryFactory()

        val statesCollection =
                {
                    val statesShapeFile =
                            {}.javaClass.getResource("/ne_10m_admin_1_states_provinces.shp")

                    val params = HashMap<String, Any>()
                    params.put("url", statesShapeFile)
                    val ds = DataStoreFinder.getDataStore(params)
                    if (ds == null) {
                        throw IOException("couldn't open " + params.get("url"))
                    }
                    SpatialIndexFeatureCollection(
                            ds.getFeatureSource(ds.getNames().get(0)).getFeatures()
                    )
                }()

        val cities =
                {
                    val cities = mutableMapOf<Int, MutableList<City>>()

                    csvReader().open({}.javaClass.getResourceAsStream("/worldcities_final.csv")) {
                        readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
                            val key = row["ne_id"]!!.toInt()
                            if (!cities.contains(key)) {
                                cities.set(key, mutableListOf<City>())
                            }
                            // ne_id,iso_a2,state,city,lat,lon
                            cities.get(key)
                                    ?.add(
                                            City(
                                                    row["city"]!!,
                                                    row["iso_a2"]!!,
                                                    row["state"]!!,
                                                    row["lat"]!!.toDouble(),
                                                    row["lon"]!!.toDouble()
                                            )
                                    )
                        }
                    }
                    cities
                }()

        private fun lookup(geometry: Geometry): SimpleFeature? {

            val filter =
                    filterFactory.intersects(
                            filterFactory.property("the_geom"),
                            filterFactory.literal(geometry)
                    )
            val features = statesCollection.subCollection(filter)
            val itr = features.features()

            try {
                if (itr.hasNext()) {
                    return itr.next()
                }
            } finally {
                itr.close()
            }
            return null
        }

        private fun getState(lat: Double, lon: Double): Triple<String, String, Int> {

            val point = geometryFactory.createPoint(Coordinate(lon, lat))
            var feature: SimpleFeature? = this.lookup(point)

            if (feature == null) {
                // if we cannot find the point let's try a polygon
                feature =
                        this.lookup(
                                {
                                    val margin = 0.1
                                    this.geometryFactory.createPolygon(
                                            arrayOf(
                                                    Coordinate(lon - margin, lat - margin),
                                                    Coordinate(lon - margin, lat + margin),
                                                    Coordinate(lon + margin, lat + margin),
                                                    Coordinate(lon + margin, lat - margin),
                                                    Coordinate(lon - margin, lat - margin)
                                            )
                                    )
                                }()
                        )
            }

            if (feature != null) {
                val buffer = Charsets.ISO_8859_1.encode(feature.getAttribute("name") as String)
                val utf8EncodedString = Charsets.UTF_8.decode(buffer).toString()
                return Triple(
                        utf8EncodedString,
                        feature.getAttribute("iso_a2").toString(),
                        feature.getAttribute("ne_id").toString().toInt()
                )
            }
            return Triple("", "", 0)
        }

        private fun getNearestCity(lat: Double, lon: Double, cities: List<City>): City? {

            val gc = GeodeticCalculator()
            val startPosition =
                    JTS.toDirectPosition(Coordinate(lon, lat), gc.coordinateReferenceSystem)
            var nearestCity: City? = null
            var minDistance = Double.POSITIVE_INFINITY
            cities.forEach {
                gc.setStartingPosition(startPosition)
                gc.setDestinationPosition(
                        JTS.toDirectPosition(
                                Coordinate(it.lon, it.lat),
                                gc.coordinateReferenceSystem
                        )
                )
                val distance = gc.getOrthodromicDistance()
                if (nearestCity == null) {
                    nearestCity = it
                    minDistance = distance
                } else if (distance < minDistance) {
                    nearestCity = it
                    minDistance = distance
                }
            }
            return nearestCity
        }

        fun findCity(tableRow: TableRow, lat: Double, lon: Double): Boolean {
            // even if is a valid address, we do not check for lat=0 and lon=0 due the high
            // probability to
            // be an error or a missing value
            if (lat == 0.0 && lon == 0.0) {
                return true
            }

            val (state, country, stateid) = getState(lat, lon)
            tableRow.set(
                    "country",
                    if (country != "") {
                        country
                    } else {
                        null
                    }
            )
            tableRow.set(
                    "region",
                    if (country != "") {
                        state
                    } else {
                        null
                    }
            )

            val stateCities = cities[stateid]
            if (stateCities != null) {
                val city = getNearestCity(lat, lon, stateCities)
                if (city != null) {
                    tableRow.set("country", city.country)
                    tableRow.set("region", city.state)
                    tableRow.set("city", city.name)
                    return true
                }
            }
            return false
        }
    }
}
