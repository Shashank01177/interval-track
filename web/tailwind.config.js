/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bgDark: '#090D16',
        surfaceDark: '#111827',
        cardDark: '#1E293B',
        borderDark: '#334155'
      }
    },
  },
  plugins: [],
}
