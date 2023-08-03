import { useCallback, useEffect, useRef, useState } from 'preact/hooks'
import DateTimePicker from '../components/DateTimePicker'
import useDb from '../hooks/useDb'
import { fetchAllHearings, fetchAvailableDates, fetchHearingsByCommitId } from '../utils/Db'
import HearingTable from '../components/HearingTable'
import Toggle from '../components/Toggle'
import HearingCard from '../components/HearingCard'
import PageNumbers from '../components/PageNumbers'

type DateHash = Record<string, string>

const PAGE_SIZE = 10

const HomeView = () => {
  const [date, setDate] = useState<string>('')
  const [minDate, setMinDate] = useState<string>('')
  const [maxDate, setMaxDate] = useState<string>('')
  const [dateMap, setDateMap] = useState<DateHash>({})

  const [hearingData, setHearingData] = useState<Hearing[]>([])
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
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
        if (currentDate.length > 0) {
          const commitId = dateMap[currentDate]
          const { hearings, totalCount } = await fetchHearingsByCommitId(
            dbRef.current,
            commitId,
            currentPage,
            PAGE_SIZE
          )
          setTotalPages(Math.ceil(totalCount / PAGE_SIZE))
          setHearingData(hearings)
        } else {
          const { hearings, totalCount } = await fetchAllHearings(dbRef.current, currentPage, PAGE_SIZE)
          setTotalPages(Math.ceil(totalCount / PAGE_SIZE))
          setHearingData(hearings)
        }
      } catch (error) {
        console.error(error)
      } finally {
        setIsFetchingHearings(false)
      }
    }
    void fetchHearings(d)
  }, [dateMap, currentPage])

  const onSetDate = useCallback((d: string) => {
    setDate(d)
  }, [])

  const onSetPage = useCallback((p: number) => {
    setCurrentPage(p)
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
                ? (
                  <>
                    <PageNumbers
                      className="mt-8"
                      currentPage={currentPage}
                      totalPages={totalPages}
                      onSetPage={onSetPage}
                    />
                    <div className="my-2 flex flex-col gap-4">
                      {hearingData.map(hearing => <HearingCard key={hearing.link} hearing={hearing} />)}
                    </div>
                    <PageNumbers
                      currentPage={currentPage}
                      totalPages={totalPages}
                      onSetPage={onSetPage}
                    />
                  </>)
                : (<HearingTable className="mt-8" hearingData={hearingData} />)}
            </div>
          </>)
      }
    </div>
  )
}

export default HomeView
