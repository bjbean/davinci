import { createSelector } from 'reselect'

const selectBizlogic = (state) => state.get('bizlogic')

const makeSelectBizlogics = () => createSelector(
  selectBizlogic,
  (bizlogicState) => bizlogicState.get('bizlogics')
)

export {
  selectBizlogic,
  makeSelectBizlogics
}
