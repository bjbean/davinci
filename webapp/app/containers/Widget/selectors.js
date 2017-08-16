import { createSelector } from 'reselect'

const selectWidget = (state) => state.get('widget')

const makeSelectWidgets = () => createSelector(
  selectWidget,
  (widgetState) => widgetState.get('widgets')
)

export {
  selectWidget,
  makeSelectWidgets
}
