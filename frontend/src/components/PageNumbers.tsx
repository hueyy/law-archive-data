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
  const minPage = currentPage <= 5 ? 0 : currentPage - 5
  const maxPage = currentPage + 6 >= totalPages ? totalPages : currentPage + 6
  return (
    <div className={`flex flex-row flex-wrap ${className}`}>
      {minPage === 0
        ? null
        : (
          <div className={'p-2 cursor-pointer'} onClick={onClick(0)}>
            1...
          </div>)}
      {[...Array(maxPage - minPage).keys()].map((_, i) => (
        <div className={`p-2 cursor-pointer ${currentPage === (minPage + i) ? 'font-bold' : ''}`} onClick={onClick(minPage + i)}>
          {minPage + i + 1}
        </div>
      ))}
      {maxPage === totalPages
        ? null
        : (
          <div className={'p-2 cursor-pointer'} onClick={onClick(totalPages - 1)}>
            ...{totalPages}
          </div>)}
    </div>
  )
}

export default PageNumbers
