import groupReducer from '../Group/reducer'
import userReducer from '../User/reducer'
import sourceReducer from '../Source/reducer'
import bizlogicReducer from '../Bizlogic/reducer'
import widgetReducer from '../Widget/reducer'
import dashboardReducer from '../Dashboard/reducer'
import reportReducer from './reducer'

export default {
  group: groupReducer,
  user: userReducer,
  source: sourceReducer,
  bizlogic: bizlogicReducer,
  widget: widgetReducer,
  dashboard: dashboardReducer,
  report: reportReducer
}
