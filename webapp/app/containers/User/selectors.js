import { createSelector } from 'reselect'

const selectUser = (state) => state.get('user')

const makeSelectUsers = () => createSelector(
  selectUser,
  (userState) => userState.get('users')
)

export {
  selectUser,
  makeSelectUsers
}
