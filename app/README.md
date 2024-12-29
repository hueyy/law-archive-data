# Frontend

The frontend consists of HTML templates served by Datasette.

## Development

### Setup via devenv (recommended)

```shell
devenv shell dev-frontend
```

### Manual setup

Alternatively, instead of using `devenv`, you can set up the dependencies manually. 

Since [TailwindCSS](https://tailwindcss.com/) is used, you will need to install [Node.JS](https://nodejs.org/en). This project uses [pnpm](https://pnpm.io) to manage its Node.JS dependencies. 

```shell
pnpm i
pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --watch
```

## Production

### Using devenv

```shell
devenv shell build-frontend
```


### Manually

```shell
pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --minify
```