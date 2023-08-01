import { useCallback, useState } from "preact/hooks"
import DateTimePicker from "./components/DateTimePicker"
import useDb from "./hooks/useDb"
import { toDateString } from "./utils/Utils"

export const App = () => {
  const today = toDateString((new Date()))
  const [date, setDate] = useState(today)
  const [minDate, setMinDate] = useState<string>('')
  const [maxDate, setMaxDate] = useState<string>('')
  const onSetDate = useCallback((d: string) => setDate(d), [])

  const onDbLoaded = useCallback((db: any) => {
    (async () => {
      try {
        const result = await db.exec(
          `SELECT hash, commit_at
            FROM commits
            ORDER BY commit_at ASC`)
        const oldestDate: string = toDateString(new Date(result[0].values[0][1]))
        const newestDate: string = toDateString(new Date(result[0].values.slice(-1)[0][1]))
        setMinDate(oldestDate)
        setMaxDate(newestDate)
      } catch (error) {
        console.error(error)
      }
    })()
  }, [])

  useDb(onDbLoaded)
  const isLoading = minDate.length === 0

  console.log(isLoading, minDate, maxDate)

  return (
    <>
      <h1>Supreme Court Hearing List</h1>
      {
        isLoading ? <span>Loading...</span> : (
          <DateTimePicker
            onChange={onSetDate}
            value={date}
            min={minDate}
            max={maxDate}
          />
        )
      }
    </>
  )
}
