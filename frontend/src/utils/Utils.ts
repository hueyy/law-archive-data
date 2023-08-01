export const ISODateTimeToLocalDate = (isoDate: string): string => isoDate.slice(0, 16)
export const toDateString = (date: Date): string => {
  const year = date.getUTCFullYear()
  const month = `${(date.getUTCMonth()+1)}`.padStart(2, `0`)
  const day = `${date.getDay()}`.padStart(2, `0`)
  return `${year}-${month}-${day}`
}