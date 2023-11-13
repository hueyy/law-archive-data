# Frontend

The frontend consists of HTML templates served by Datasette.

## Development

Since [TailwindCSS](https://tailwindcss.com/) is used, you will need to install [Node.JS](https://nodejs.org/en). This project uses [pnpm](https://pnpm.io) to manage its Node.JS dependencies. 

```bash
pnpm i
pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --watch
```

## Production

```
pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --minify
```