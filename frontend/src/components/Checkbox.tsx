import type { ComponentChild } from 'preact'
import type { ChangeEvent } from 'preact/compat'

interface Props {
  className?: string
  checked: boolean
  onChange?: (event: ChangeEvent<HTMLInputElement>) => void
  children: ComponentChild
}

const Checkbox: React.FC<Props> = ({
  className = '',
  checked,
  onChange = () => { },
  children = ''
}) => {
  return (
    <div className="">
      <input
        className={`cursor-pointer ${className}`}
        type="checkbox"
        checked={checked}
        onChange={onChange}
      />
      <label>
        {children}
      </label>
    </div>

  )
}

export default Checkbox
