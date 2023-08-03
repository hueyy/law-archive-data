import { Router, Route } from 'preact-router'
import HomeView from './views/Home'
import AboutView from './views/About'

export const App = () => {
  return (
    <Router>
      <Route default path="/" component={HomeView} />
      <Route path="/about" component={AboutView} />
    </Router>
  )
}
