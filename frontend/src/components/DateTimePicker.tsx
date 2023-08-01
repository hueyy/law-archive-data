import { useCallback } from "preact/hooks"
import type { FunctionComponent } from 'preact'

interface Props {
  onChange: (value: string) => void,
  value: string,
  min?: string,
  max?: string,
  type?: string,
}

const DateTimePicker: FunctionComponent<Props> = ({
  onChange,
  value,
  min,
  max,
  type = `date`,
}) => {
  const onChangeInternal = useCallback((event: Event) => {
    const { target } = event
    onChange((target as HTMLInputElement).value)
  }, [])
  return (
    <input
      type={type}
      min={min}
      max={max}
      onChange={onChangeInternal}
      value={value}
    ></input>
  )
}

export default DateTimePicker