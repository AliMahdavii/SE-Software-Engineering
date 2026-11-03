-- SQLite schema for the Library Management System.
-- This file is for reference only; DatabaseHandler.java creates the
-- schema automatically on startup. You can also apply it manually with:
--     sqlite3 data/library.db < schema.sql

-- Books in the catalog. The "available" flag is derived at runtime
-- from the borrows table (a book is unavailable if it has an
-- unreturned borrow record), so it is not stored here.
CREATE TABLE IF NOT EXISTS books (
    id        INTEGER PRIMARY KEY,
    title     TEXT    NOT NULL,
    author    TEXT    NOT NULL,
    year      INTEGER NOT NULL
);

-- Registered students.
CREATE TABLE IF NOT EXISTS students (
    id              INTEGER PRIMARY KEY,
    name            TEXT    NOT NULL,
    student_number  TEXT    NOT NULL UNIQUE
);

-- Borrow records. A (student_id, book_id, borrow_date) tuple is unique
-- so a student can borrow the same book again after returning it, but
-- never twice on the same day.
CREATE TABLE IF NOT EXISTS borrows (
    student_id   INTEGER NOT NULL,
    book_id      INTEGER NOT NULL,
    borrow_date  TEXT    NOT NULL,
    returned     INTEGER NOT NULL,
    PRIMARY KEY (student_id, book_id, borrow_date),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (book_id)    REFERENCES books(id)
);
