import type { FunctionComponent } from 'preact'
import { useCallback } from 'preact/hooks'

interface Props {
  totalPages: number
  currentPage: number
  className?: string
  onSetPage: (p: number) => void
}

const PageNumbers: FunctionComponent<Props> = ({
  totalPages,
  currentPage,
  onSetPage,
  className = ''
}) => {
  const onClick = useCallback((pageNumber: number) => () => {
    onSetPage(pageNumber)
  }, [onSetPage])
  return (
    <div className={`flex flex-row flex-wrap ${className}`}>
      {[...Array(totalPages).keys()].map((_, i) => (
        <div className={`p-2 cursor-pointer ${currentPage === i ? 'font-bold' : ''}`} onClick={onClick(i)}>
          {i + 1}
        </div>
      ))}
    </div>
  )
}

export default PageNumbers
