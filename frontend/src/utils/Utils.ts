export const ISODateTimeToLocalDate = (isoDate: string): string => isoDate.slice(0, 16)
export const toDateString = (date: Date): string => {
  const sgDate = date.toLocaleString('en-SG', { timeZone: 'Asia/Singapore' })
  const day = sgDate.slice(0, 2)
  const month = sgDate.slice(3, 5)
  const year = sgDate.slice(6, 10)
  // const year = date.getUTCFullYear()
  // const month = `${(date.getUTCMonth()+1)}`.padStart(2, `0`)
  // const day = `${date.getDay()}`.padStart(2, `0`)
  return `${year}-${month}-${day}`
}
export const getYesterdayDate = (): Date => {
  const d = new Date()
  d.setDate(d.getDate() - 1)
  return d
}
