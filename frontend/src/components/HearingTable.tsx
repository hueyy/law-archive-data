import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import type { FunctionComponent } from 'preact'

interface Props {
  hearingData: Hearing[]
  className?: string
}

const HearingTable: FunctionComponent<Props> = ({
  hearingData,
  className = ''
}) => {
  const columnHelper = createColumnHelper<Hearing>()
  const columns = [
    {
      accessorKey: 'title'
    },
    columnHelper.accessor('parties', {
      cell: (props) => (
        <div className="flex flex-col gap-2">
          {props.getValue().map(party => (
            <div>
              {party.role}: {party.name.replace('(represented)', '')}
              {party.representation !== null
                ? <> (Represented by: {party.representation})</>
                : <> (unrepresented)</>}
            </div>
          ))}
        </div>
      )
    }),
    {
      accessorKey: 'venue'
    },
    {
      accessorKey: 'coram'
    },
    {
      accessorKey: 'timestamp'
    },
    columnHelper.accessor('link', {
      cell: (props) => (
        <a href={props.row.original.link}>{props.getValue().split('/').slice(-1)[0]}</a>
      )
    })
  ]

  const table = useReactTable({
    data: hearingData,
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return (
    <table className={className}>
      <thead>
        {table.getHeaderGroups().map(headerGroup => (
          <tr key={headerGroup.id}>
            {headerGroup.headers.map(header => (
              <th key={header.id}>
                {header.isPlaceholder
                  ? null
                  : flexRender(
                    header.column.columnDef.header,
                    header.getContext()
                  )}
              </th>
            ))}
          </tr>
        ))}
      </thead>
      <tbody>
        {table.getRowModel().rows.map(row => (
          <tr key={row.id}>
            {row.getVisibleCells().map(cell => (
              <td className="p-3 border border-black" key={cell.id}>
                {flexRender(
                  cell.column.columnDef.cell,
                  cell.getContext()
                )}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
      <tfoot>
        {table.getFooterGroups().map(footerGroup => (
          <tr key={footerGroup.id}>
            {footerGroup.headers.map(header => (
              <th key={header.id}>
                {header.isPlaceholder
                  ? null
                  : flexRender(
                    header.column.columnDef.footer,
                    header.getContext()
                  )}
              </th>
            ))}
          </tr>
        ))}
      </tfoot>
    </table>
  )
}

export default HearingTable
