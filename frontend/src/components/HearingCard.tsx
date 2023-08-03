import type { FunctionComponent } from 'preact'

interface Props {
  hearing: Hearing
  className?: string
}

const HearingCard: FunctionComponent<Props> = ({
  hearing,
  className = ''
}) => {
  return (
    <div className={`p-4 border border-gray-300 ${className}`}>
      <div className="flex flex-row justify-between text-sm">
        <div className="">
          {hearing.timestamp.toTimeString().slice(0, 5)} / {hearing.reference}
        </div>
        <div className="">
          {hearing.type}
        </div>
      </div>
      <div className="my-3">
        <h2 className="font-bold text-xl">{hearing.title}</h2>
      </div>
      <div className="flex flex-row justify-between text-sm">
        <div>
          <p className="text-gray-500">Venue</p>
          <p>{hearing.venue}</p>
        </div>
        <div>
          <p className="text-gray-500">Coram</p>
          <p>{hearing.coram}</p>
        </div>
      </div>
    </div>
  )
}

export default HearingCard
