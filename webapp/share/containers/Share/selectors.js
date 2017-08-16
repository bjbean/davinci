import { createSelector } from 'reselect'

const selectShare = (state) => state.get('share')

const makeSelectItems = () => createSelector(
  selectShare,
  (shareState) => shareState.get('items')
)

export {
  selectShare,
  makeSelectItems
}
