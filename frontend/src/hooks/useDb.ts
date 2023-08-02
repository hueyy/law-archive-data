import { createDbWorker } from "sql.js-httpvfs"
import { useEffect, useRef } from "preact/hooks"
import Db from '../utils/Db'


const useDb = (done: (db: any) => void) => {
  const workerDb = useRef<any>(null)

  useEffect(() => {
    if(workerDb.current !== null){
      done(workerDb.current)
      return
    }
    (async () => {
      try {
        const worker = await createDbWorker(
          [Db.config],
          Db.workerUrl.toString(),
          Db.wasmUrl.toString()
        )

        workerDb.current = worker.db
        done(workerDb.current)
      } catch (error){
        console.error(error)
      }   
    })()
  }, [workerDb])
}

export default useDb