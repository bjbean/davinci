import { createSelector } from 'reselect'

const selectSource = (state) => state.get('source')

const makeSelectSources = () => createSelector(
  selectSource,
  (sourceState) => sourceState.get('sources')
)

export {
  selectSource,
  makeSelectSources
}
