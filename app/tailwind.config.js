const defaultTheme = require("tailwindcss/defaultTheme");

/** @type {import('tailwindcss').Config} */
module.exports = {
	content: ["./html-templates/**/*.html", "./src/**/*.cljs"],
	theme: {
		extend: {
			fontFamily: {
				sans: ["Fira Sans", ...defaultTheme.fontFamily.sans],
			},
		},
	},
	plugins: [],
};
