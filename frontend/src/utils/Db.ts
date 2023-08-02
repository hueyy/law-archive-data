const workerUrl = new URL(
  'sql.js-httpvfs/dist/sqlite.worker.js',
  import.meta.url
)

const wasmUrl = new URL(
  'sql.js-httpvfs/dist/sql-wasm.wasm',
  import.meta.url
)

const config = {
  from: 'jsonconfig',
  configUrl: '/db/config.json'
  // from: 'inline' as const,
  // config: {
  //   serverMode: 'full' as const,
  //   requestChunkSize: 4096, // the page size of the  sqlite database (by default 4096)
  //   url: '/hearings.db'
  // }
}

const DbConfig = {
  workerUrl,
  wasmUrl,
  config
}

export default DbConfig
