import { createSelector } from 'reselect'

const selectWidget = (state) => state.get('widget')

const makeSelectWidgets = () => createSelector(
  selectWidget,
  (widgetState) => widgetState.get('widgets')
)

const makeSelectWidgetlibs = () => createSelector(
  selectWidget,
  (widgetState) => widgetState.get('widgetlibs')
)

export {
  selectWidget,
  makeSelectWidgets,
  makeSelectWidgetlibs
}
