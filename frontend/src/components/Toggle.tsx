import type { FunctionComponent } from 'preact'
import { useCallback } from 'preact/hooks'

interface Props {
  options: string[]
  value: string
  onChange: (value: string) => void
  className?: string
}

const Toggle: FunctionComponent<Props> = ({
  options,
  value,
  onChange,
  className = ''
}) => {
  const onChangeSelection = useCallback((option: string) => () => {
    onChange(option)
  }, [onChange])

  const divClass = 'px-2 py-0.5 cursor-pointer flex items-center'
  const activeClass = 'bg-slate-600 text-white'

  return (
    <div className={`inline-flex flex-row justify-start border border-solid border-gray-400 w-fit rounded ${className}`}>

      {options.map(option => (
        <div
          className={`${divClass} ${value === option ? activeClass : ''}`}
          onClick={onChangeSelection(option)}
          key={option}
        >
          {option}
        </div>
      ))}
    </div>
  )
}

export default Toggle
