package main

import (
	"context"
	"database/sql"
	"flag"
	"fmt"
	"os"
	"reflect"
	"time"

	"github.com/apache/beam/sdks/v2/go/pkg/beam"
	"github.com/apache/beam/sdks/v2/go/pkg/beam/io/bigqueryio"
	"github.com/apache/beam/sdks/v2/go/pkg/beam/log"
	"github.com/pkg/errors"
	"github.com/apache/beam/sdks/v2/go/pkg/beam/options/gcpopts"
	"github.com/apache/beam/sdks/v2/go/pkg/beam/x/beamx"
	_ "github.com/lib/pq"
)

var (
	pgconn = flag.String("pgconn", "", "postgres connection string (required).")
)

func init() {
	beam.RegisterType(reflect.TypeOf((*writePostgresFn)(nil)).Elem())
}

type bqRecord struct {
	Timestamp time.Time `json:"timestamp"`
	UserID      int       `json:"userid"`
}

type writePostgresFn struct {
	Dsn   string `json:"dsn"`
	db    *sql.DB
	stmt  *sql.Stmt
	tx    *sql.Tx
	Table string `json:"table"`
}

// Setup
// StartBundle
// FinishBundle
// Teardown

func (f *writePostgresFn) Setup(ctx context.Context) error {
	// crate the db connection
	db, err := sql.Open("postgres", f.Dsn)
	if err != nil {
		return errors.Wrap(err, "failed to open database")
	}
	f.db = db

	return nil
}

func (f *writePostgresFn) StartBundle(ctx context.Context) error {
	tx, err := f.db.Begin()
	if err != nil {
		return errors.Wrap(err, "failed to open transaction")
	}

	stmt, err := tx.PrepareContext(ctx, "INSERT INTO users_log(timestamp,userid) VALUES($1,$2)")
	if err != nil {
		return errors.Wrap(err, "failed to prepare statement")
	}
	f.tx = tx
	f.stmt = stmt
	return nil
}

func (f *writePostgresFn) ProcessElement(ctx context.Context, elem bqRecord) error {
	log.Infof(ctx, "elem %v", elem)
	_, err := f.stmt.Exec(elem.Timestamp, elem.UserID)
	if err != nil {
		return errors.Wrapf(err, "failed to execute statement for element %v", elem)
	}
	return nil
}

func (f *writePostgresFn) FinishBundle(ctx context.Context) error {
	if err := f.stmt.Close(); err != nil {
		return errors.Wrap(err, "failed to close statement")
	}

	if err := f.tx.Commit(); err != nil {
		return errors.Wrap(err, "failed to commit transaction")
	}

	log.Infof(ctx, "from FinishBundle %v", true)
	return nil
}

func (f *writePostgresFn) Teardown(ctx context.Context) error {
	f.db.Close()
	return nil
}

func main() {

	flag.Parse()
	beam.Init()

	if *pgconn == "" {
		fmt.Println("pgconn is required")
		os.Exit(1)
	}

	p := beam.NewPipeline()
	s := p.Root()

	query := "SELECT timestamp, userid FROM `demo_bq_dataset.users_log`"

	project := gcpopts.GetProject(context.Background())

	rows := bigqueryio.Query(s, project, query, reflect.TypeOf(bqRecord{}), bigqueryio.UseStandardSQL())

	beam.ParDo0(s, &writePostgresFn{Dsn: *pgconn}, rows)

	if err := beamx.Run(context.Background(), p); err != nil {
		fmt.Printf("Failed to execute job: %v", err)
		os.Exit(1)
	}
}
