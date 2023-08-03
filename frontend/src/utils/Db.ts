import { toDateString } from './Utils'

const workerUrl = new URL(
  'sql.js-httpvfs/dist/sqlite.worker.js',
  import.meta.url
)

const wasmUrl = new URL(
  'sql.js-httpvfs/dist/sql-wasm.wasm',
  import.meta.url
)

const config = {
  // from: 'jsonconfig',
  // configUrl: '/db/config.json'
  from: 'inline' as const,
  config: {
    serverMode: 'full' as const,
    requestChunkSize: 4096, // the page size of the  sqlite database (by default 4096)
    url: '/db/hearings.db'
  }
}

export const fetchAvailableDates = async (db: any): Promise<{
  dateMap: any
  oldestDate: string
  newestDate: string
}> => {
  try {
    const result = await db.exec(
          `SELECT id, commit_at
            FROM commits
            ORDER BY commit_at ASC`)
    const data = result[0].values.map(
      ([_, timestamp]: [_: never, timestamp: string]) =>
        [_, toDateString((new Date(timestamp)))])
    return {
      dateMap: [
        ...new Set(data.map(([_, timestamp]: [_: never, timestamp: string]) => timestamp))
      ].map(timestamp => data.find(([_, t]: [_: never, t: string]) => timestamp === t))
        .reduce((prev, [id, date]: [hash: string, date: string]) => ({
          ...prev,
          [date]: id
        }), {}),
      oldestDate: toDateString(new Date(data[0][1])),
      newestDate: toDateString(new Date(data.slice(-1)[0][1]))
    }
  } catch (error) {
    console.error(error)
    throw error
  }
}

export const fetchHearingsByCommitId = async (
  db: any,
  commitId: string,
  pageSize: number = 50
) => {
  try {
    const result = await db.exec(
      'SELECT * FROM item WHERE _commit = ? LIMIT ?',
      [commitId, pageSize]
    )
    const hearings = (result[0].values.map((v: string[]): Hearing => ({
      chargeNumber: v[0],
      natureOfCase: v[1],
      parties: JSON.parse(v[2]),
      type: v[3],
      title: v[4],
      hearingOutcome: v[5],
      reference: v[6],
      link: v[7],
      venue: v[8],
      hearingType: v[9],
      timestamp: new Date(v[10]),
      offenceDescription: v[11],
      coram: v[12]
    })))
    return hearings
  } catch (error) {
    console.error(error)
    throw error
  }
}

const Db = {
  workerUrl,
  wasmUrl,
  config
}

export default Db
