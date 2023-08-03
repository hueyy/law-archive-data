import { useCallback, useEffect, useRef, useState } from 'preact/hooks'
import DateTimePicker from './components/DateTimePicker'
import useDb from './hooks/useDb'
import { fetchAvailableDates, fetchHearingsByCommitId } from './utils/Db'
import { getYesterdayDate, toDateString } from './utils/Utils'
import HearingTable from './components/HearingTable'
import Toggle from './components/Toggle'
import HearingCard from './components/HearingCard'

type DateHash = Record<string, string>

export const App = () => {
  const today = toDateString(getYesterdayDate())
  const [date, setDate] = useState(today)
  const [minDate, setMinDate] = useState<string>('')
  const [maxDate, setMaxDate] = useState<string>('')
  const [dateMap, setDateMap] = useState<DateHash>({})

  const [hearingData, setHearingData] = useState<Hearing[]>([])
  const [isFetchingHearings, setIsFetchingHearings] = useState(true)
  const [viewMode, setViewMode] = useState<ViewMode>('Simple')

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
        setIsFetchingHearings(true)
        const commitId = dateMap[currentDate]
        const hearings = await fetchHearingsByCommitId(
          dbRef.current,
          commitId
        )
        setHearingData(hearings)
      } catch (error) {
        console.error(error)
      } finally {
        setIsFetchingHearings(false)
      }
    }
    void fetchHearings(d)
  }, [dateMap])

  const onSetDate = useCallback((d: string) => {
    setDate(d)
  }, [])

  const onDbLoaded = useCallback((db: any) => {
    const fetch = async () => {
      try {
        const {
          dateMap,
          oldestDate,
          newestDate
        } = await fetchAvailableDates(db)
        setDateMap(dateMap)
        setMinDate(oldestDate)
        setMaxDate(newestDate)
        setDate(newestDate)
        dbRef.current = db
      } catch (error) {
        console.error(error)
      }
    }
    void fetch()
  }, [])

  useDb(onDbLoaded)
  const isLoading = minDate.length === 0 || isFetchingHearings

  useEffect(() => {
    getHearings(date)
  }, [getHearings, date])

  return (
    <div className="container max-w-screen-lg p-6 mx-auto">
      <h1 className="mb-6 text-center text-xl font-bold">
        SG Courts Hearing List
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
                className="ml-4"
                options={['Simple', 'Advanced']}
                value={viewMode}
                onChange={onChangeViewMode}
              />
            </div>

            <div>
              {viewMode === 'Simple'
                ? (<div className="mt-8 flex flex-col gap-4">
                  {hearingData.map(hearing => <HearingCard key={hearing.link} hearing={hearing} />)}
                </div>)
                : (<HearingTable className="mt-8" hearingData={hearingData} />)}
            </div>
          </>)
      }
    </div>
  )
}
