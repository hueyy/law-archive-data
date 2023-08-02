interface Party {
  name: string
  representation: string | null
  role: string
}

interface Hearing {
  chargeNumber?: string
  natureOfCase?: string
  parties: Party[]
  type: string
  title: string
  hearingOutcome?: string
  reference?: string
  link: string
  venue?: string
  hearingType?: string
  timestamp: Date
  offenceDescription?: string
  coram?: string
}

type ViewMode = 'simple' | 'advanced'
