Vendored from the official `@sqlite.org/sqlite-wasm` npm package, version 3.53.0-build1.

Only the browser build is kept: `sqlite3.mjs` (renamed from `dist/index.mjs`), the `sqlite3.wasm`
it loads, and the OPFS async proxy it looks for. Refresh by re-copying those three files from a
newer release of the same package.
