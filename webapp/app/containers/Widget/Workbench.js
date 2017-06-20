import React, { PropTypes } from 'react'
import { connect } from 'react-redux'

import WidgetForm from './WidgetForm'
import SplitView from './SplitView'

import { loadBizdatas } from '../Bizlogic/actions'
import { addWidget, editWidget } from './actions'
import { promiseDispatcher } from '../../utils/reduxPromisation'

import styles from './Widget.less'

export class Workbench extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      dataSource: false,
      chartInfo: false,
      chartParams: {},
      tableLoading: false,
      olapSql: props.type === 'edit' ? props.widget.olap_sql : '',

      tableHeight: 0
    }
  }

  componentWillMount () {
    if (this.props.type === 'edit') {
      this.getDetail(this.props)
    }
  }

  componentDidMount () {
    this.setState({
      chartParams: this.widgetForm.getFieldsValue()
    })
  }

  componentWillUpdate (nextProps) {
    const type = nextProps.type
    const widget = nextProps.widget || {}
    const currentWidget = this.props.widget || {}

    this.state.olapSql = widget.olap_sql

    if (widget.id !== currentWidget.id && type === 'edit') {
      this.getDetail(nextProps)
    }
  }

  getDetail = (props) => {
    const { widget } = props
    const { olapSql } = this.state
    this.bizlogicChange(widget.flatTable_id, olapSql)
    this.widgetTypeChange(widget.widgetlib_id)
      .then(() => {
        const info = {
          id: widget.id,
          name: widget.name,
          desc: widget.desc,
          flatTable_id: `${widget.flatTable_id}`,
          widgetlib_id: `${widget.widgetlib_id}`
        }

        const params = JSON.parse(widget.chart_params)

        delete params.widgetName
        delete params.widgetType

        const formValues = Object.assign({}, info, params)

        this.setState({
          chartParams: formValues
        })
        this.widgetForm.setFieldsValue(formValues)
      })
  }

  bizlogicChange = (val, sql) => {
    this.setState({ tableLoading: true })
    this.props.onLoadBizdatas(val, sql)
      .then(resultset => {
        console.log(resultset)
        this.setState({
          dataSource: resultset.dataSource,
          tableLoading: false
        })
      })
      .catch(() => {
        this.setState({ tableLoading: false })
      })
  }

  olapSqlQuery = () => {
    const flatTableId = this.widgetForm.getFieldValue('flatTable_id')
    if (flatTableId) {
      this.bizlogicChange(flatTableId, this.state.olapSql)
    }
  }

  widgetTypeChange = (val) =>
    new Promise((resolve) => {
      this.setState({
        chartInfo: this.props.widgetlibs.find(wl => wl.id === Number(val))
      }, () => {
        resolve()
      })
    })

  formItemChange = (field) => (val) => {
    this.setState({
      chartParams: Object.assign({}, this.state.chartParams, { [field]: val })
    })
  }

  saveWidget = () => new Promise((resolve, reject) => {
    this.widgetForm.validateFieldsAndScroll((err, values) => {
      if (!err) {
        const { chartInfo, olapSql } = this.state

        let id = values.id
        let name = values.name
        let desc = values.desc
        let widgetlib_id = Number(values.widgetlib_id)  // eslint-disable-line
        let flatTable_id = Number(values.flatTable_id)  // eslint-disable-line

        delete values.id
        delete values.name
        delete values.desc
        delete values.widgetlib_id
        delete values.flatTable_id

        let widget = {
          name,
          desc,
          olap_sql: olapSql,
          publish: true,
          trigger_type: '',
          widgetlib_id,
          chart_params: JSON.stringify(Object.assign({}, values, {
            widgetName: chartInfo.name,
            widgetType: chartInfo.type
          })),
          trigger_params: '',
          flatTable_id
        }

        if (this.props.type === 'edit') {
          widget.id = id
          this.props.onEditWidget(widget).then(() => {
            resolve()
            this.resetWorkbench()
            this.props.onClose()
          })
        } else {
          this.props.onAddWidget(widget).then(() => {
            resolve()
            this.resetWorkbench()
            this.props.onClose()
          })
        }
      } else {
        reject()
      }
    })
  })

  resetWorkbench = () => {
    this.widgetForm.resetFields()
    this.setState({
      dataSource: false,
      chartInfo: false,
      chartParams: {},
      olapSql: ''
    })
  }

  olapSqlInputChange = (event) => {
    this.setState({
      olapSql: event.target.value
    })
  }

  render () {
    const {
      bizlogics,
      widgetlibs
    } = this.props
    const {
      dataSource,
      chartInfo,
      chartParams,
      tableLoading,
      olapSql
    } = this.state

    return (
      <div className={`${styles.workbench} no-item-margin`}>
        <WidgetForm
          bizlogics={bizlogics}
          widgetlibs={widgetlibs}
          dataSource={dataSource}
          chartInfo={chartInfo}
          onBizlogicChange={this.bizlogicChange}
          onWidgetTypeChange={this.widgetTypeChange}
          onFormItemChange={this.formItemChange}
          ref={f => { this.widgetForm = f }}
        />
        <SplitView
          dataSource={dataSource}
          chartInfo={chartInfo}
          chartParams={chartParams}
          tableLoading={tableLoading}
          olapSql={olapSql}
          onSaveWidget={this.saveWidget}
          onOlapSqlInputChange={this.olapSqlInputChange}
          onOlapSqlQuery={this.olapSqlQuery}
        />
      </div>
    )
  }
}

Workbench.propTypes = {
  type: PropTypes.string,
  widget: PropTypes.object,
  bizlogics: PropTypes.array,
  widgetlibs: PropTypes.array,
  onLoadBizdatas: PropTypes.func,
  onAddWidget: PropTypes.func,
  onEditWidget: PropTypes.func,
  onClose: PropTypes.func
}

export function mapDispatchToProps (dispatch) {
  return {
    onLoadBizdatas: (id, sql) => promiseDispatcher(dispatch, loadBizdatas, id, sql, undefined, undefined),
    onAddWidget: (widget) => promiseDispatcher(dispatch, addWidget, widget),
    onEditWidget: (widget) => promiseDispatcher(dispatch, editWidget, widget)
  }
}

export default connect(null, mapDispatchToProps, null, {withRef: true})(Workbench)
