import { useCallback, useEffect, useRef, useState } from 'preact/hooks'
import DateTimePicker from './components/DateTimePicker'
import useDb from './hooks/useDb'
import { getYesterdayDate, toDateString } from './utils/Utils'
import HearingTable from './components/HearingTable'
import Toggle from './components/Toggle'

type DateHash = Record<string, string>

export const App = () => {
  const today = toDateString(getYesterdayDate())
  const [date, setDate] = useState(today)
  const [minDate, setMinDate] = useState<string>('')
  const [maxDate, setMaxDate] = useState<string>('')
  const [dateMap, setDateMap] = useState<DateHash>({})
  const [hearingData, setHearingData] = useState<Hearing[]>([])
  const [viewMode, setViewMode] = useState<ViewMode>('simple')

  const dbRef = useRef<any>(null)

  const onChangeViewMode = useCallback((newViewMode: string) => {
    setViewMode(newViewMode as ViewMode)
  }, [])

  const getHearings = useCallback((d: string) => {
    if (dbRef.current === null || Object.values(dateMap).length === 0) {
      // not yet ready
      return
    }
    const fetchHearings = async (currentDate: string) => {
      try {
        const commitId = dateMap[currentDate]
        const result = await dbRef.current.exec(
          'SELECT * FROM item WHERE _commit = ?',
          [commitId]
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
        setHearingData(hearings)
      } catch (error) {
        console.error(error)
      }
    }
    void fetchHearings(d)
  }, [dateMap])

  const onSetDate = useCallback((d: string) => {
    setDate(d)
  }, [])

  const onDbLoaded = useCallback((db: any) => {
    const fetchAvailableDates = async () => {
      try {
        const result = await db.exec(
          `SELECT id, commit_at
            FROM commits
            ORDER BY commit_at ASC`)
        const data = result[0].values.map(([_, timestamp]: [_: never, timestamp: string]) => [_, toDateString((new Date(timestamp)))])
        setDateMap(
          [
            ...new Set(data.map(([_, timestamp]: [_: never, timestamp: string]) => timestamp))
          ].map(timestamp => data.find(([_, t]: [_: never, t: string]) => timestamp === t))
            .reduce((prev, [id, date]: [hash: string, date: string]) => ({
              ...prev,
              [date]: id
            }), {})
        )
        const oldestDate: string = toDateString(new Date(data[0][1]))
        const newestDate: string = toDateString(new Date(data.slice(-1)[0][1]))
        setMinDate(oldestDate)
        setMaxDate(newestDate)
        dbRef.current = db
      } catch (error) {
        console.error(error)
      }
    }
    void fetchAvailableDates()
  }, [])

  useDb(onDbLoaded)
  const isLoading = minDate.length === 0

  useEffect(() => {
    getHearings(date)
  }, [getHearings, date])

  return (
    <div className="container max-w-screen-lg p-6 mx-auto">
      <h1 className="mb-6">
        Supreme Court Hearing List
      </h1>
      {
        isLoading
          ? <span>Loading...</span>
          : (<>
            <div>
              <DateTimePicker
                onChange={onSetDate}
                value={date}
                min={minDate}
                max={maxDate}
              />
              <Toggle
                options={['Simple', 'Advanced']}
                value={viewMode}
                onChange={onChangeViewMode}
              />
            </div>

            <div>
              {viewMode === 'simple'
                ? (<>
                  Simple
                </>)
                : (
                  <HearingTable hearingData={hearingData} />
                )}
            </div>
          </>)
      }
    </div>
  )
}
