import { createSelector } from 'reselect'

const selectGroup = (state) => state.get('group')

const makeSelectGroups = () => createSelector(
  selectGroup,
  (groupState) => groupState.get('groups')
)

export {
  selectGroup,
  makeSelectGroups
}
