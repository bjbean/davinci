import { createSelector } from 'reselect'

const selectDashboard = (state) => state.get('dashboard')

const makeSelectDashboards = () => createSelector(
  selectDashboard,
  (widgetState) => widgetState.get('dashboards')
)

const makeSelectCurrentDashboard = () => createSelector(
  selectDashboard,
  (widgetState) => widgetState.get('currentDashboard')
)
const makeSelectCurrentItems = () => createSelector(
  selectDashboard,
  (widgetState) => widgetState.get('currentItems')
)

export {
  selectDashboard,
  makeSelectDashboards,
  makeSelectCurrentDashboard,
  makeSelectCurrentItems
}
