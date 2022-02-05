package geolocation

import com.google.api.services.bigquery.model.TableRow
import java.util.stream.Stream
import kotlin.test.assertEquals
import org.apache.beam.sdk.testing.TestPipeline
import org.junit.Assert.*
import org.junit.Rule
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory

class CityLocatorTests {

  @Rule
  @JvmField
  @Transient
  val pipeline: TestPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(false)

  @ParameterizedTest(name = "{0}")
  @MethodSource("providesTestCityLocatorValues")
  fun testCityLocator(desc: String, latitude: Double, longitude: Double, expected: TableRow) {
    logger.info("Run Test {}...", desc)
    val tableRow = TableRow()

    CityLocator.findCity(tableRow, latitude, longitude)
    assertEquals(expected, tableRow)
  }

  companion object {
    @JvmStatic val logger = LoggerFactory.getLogger(CityLocatorTests::class.java)

    @JvmStatic
    fun providesTestCityLocatorValues(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                "find city - at the country border",
                43.8357503839297,
                25.90642278799463,
                TableRow().set("country_code", "RO").set("state", "Giurgiu").set("city", "Giurgiu")
            ),
            Arguments.of(
                "find city - at the state border",
                47.24988979287402,
                -1.5580190308221318,
                TableRow()
                    .set("country_code", "FR")
                    .set("state", "Loire-Atlantique")
                    .set("city", "Nantes")
            ),
            Arguments.of(
                "find city - with unicode characters",
                -23.5504,
                -46.6339,
                TableRow()
                    .set("country_code", "BR")
                    .set("state", "São Paulo")
                    .set("city", "São Paulo")
            ),
            Arguments.of(
                "find city - simple test",
                37.7611095292885,
                -122.46962037232535,
                TableRow()
                    .set("country_code", "US")
                    .set("state", "California")
                    .set("city", "San Francisco")
            ),
            Arguments.of(
                "find city - approximate location",
                37.21092917860548,
                -121.81520925344503,
                TableRow()
                    .set("country_code", "US")
                    .set("state", "California")
                    .set("city", "San Jose")
            ),
            Arguments.of(
                "find city - find the nearest city",
                25.736750231345077,
                -80.23469396286302,
                TableRow().set("country_code", "US").set("state", "Florida").set("city", "Miami")
            ),
            Arguments.of(
                "find city - approximate location",
                37.7500475858368,
                -122.52663085973825,
                TableRow()
                    .set("country_code", "US")
                    .set("state", "California")
                    .set("city", "San Francisco")
            )
        )
  }
}
