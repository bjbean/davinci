import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router'

import Workbench from './Workbench'
import Container from '../../components/Container'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Button from 'antd/lib/button'
import Icon from 'antd/lib/icon'
import Modal from 'antd/lib/modal'
import Popconfirm from 'antd/lib/popconfirm'
import Breadcrumb from 'antd/lib/breadcrumb'

import { promiseDispatcher } from '../../utils/reduxPromisation'
import { loadWidgets, loadWidgetlibs, deleteWidget } from './actions'
import { makeSelectWidgets, makeSelectWidgetlibs } from './selectors'
import { loadBizlogics } from '../Bizlogic/actions'
import { makeSelectBizlogics } from '../Bizlogic/selectors'
import chartIconMapping from './chartIconMapping'

import styles from './Widget.less'
import utilStyles from '../../assets/less/util.less'

export class Widget extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      workbenchType: '',
      currentWidget: null,
      workbenchVisible: false
    }
  }

  componentWillMount () {
    const {
      onLoadWidgets,
      onLoadBizlogics,
      onLoadWidgetlibs
    } = this.props

    onLoadWidgets()
    onLoadBizlogics()
    onLoadWidgetlibs()
  }

  showWorkbench = (type, widget) => () => {
    this.setState({
      workbenchType: type,
      currentWidget: widget,
      workbenchVisible: true
    })
  }

  hideWorkbench = () => {
    this.setState({
      workbenchVisible: false,
      workbenchType: '',
      currentWidget: null
    })
  }

  afterModalClose = () => {
    this.workbenchWrapper.refs.wrappedInstance.resetWorkbench()
  }

  stopPPG = (e) => {
    e.stopPropagation()
  }

  render () {
    const {
      widgets,
      bizlogics,
      widgetlibs,
      onDeleteWidget
    } = this.props

    const {
      workbenchType,
      currentWidget,
      workbenchVisible
    } = this.state

    const cols = widgets
      ? widgets.map(w => {
        const widgetType = JSON.parse(w.chart_params).widgetType
        return (
          <Col
            xl={4} lg={6} md={8} sm={12} xs={24}
            key={w.id}
            onClick={this.showWorkbench('edit', w)}
          >
            <div className={styles.widget}>
              <h3 className={styles.title}>{w.name}</h3>
              <p className={styles.content}>{w.desc}</p>
              <i className={`${styles.pic} iconfont ${chartIconMapping[widgetType]}`} />
              <Popconfirm
                title="确定删除？"
                placement="bottom"
                onConfirm={onDeleteWidget(w.id)}
              >
                <Icon className={styles.delete} type="delete" onClick={this.stopPPG} />
              </Popconfirm>
            </div>
          </Col>
        )
      })
      : ''

    return (
      <Container>
        <Container.Title>
          <Row>
            <Col span={18}>
              <Breadcrumb className={utilStyles.breadcrumb}>
                <Breadcrumb.Item>
                  <Link>Widget</Link>
                </Breadcrumb.Item>
              </Breadcrumb>
            </Col>
            <Col span={6} className={utilStyles.textAlignRight}>
              <Button
                size="large"
                type="primary"
                icon="plus"
                onClick={this.showWorkbench('add')}
              >
                新 增
              </Button>
            </Col>
          </Row>
        </Container.Title>
        <Container.Body>
          <Row gutter={20}>
            {cols}
          </Row>
        </Container.Body>
        <Modal
          title={`${workbenchType === 'add' ? '新增' : '修改'} Widget`}
          wrapClassName={`ant-modal-xlarge ${styles.workbenchWrapper}`}
          visible={workbenchVisible}
          onCancel={this.hideWorkbench}
          afterClose={this.afterModalClose}
          footer={false}
          maskClosable={false}
        >
          <Workbench
            type={workbenchType}
            widget={currentWidget}
            bizlogics={bizlogics || []}
            widgetlibs={widgetlibs || []}
            onClose={this.hideWorkbench}
            ref={f => { this.workbenchWrapper = f }}
          />
        </Modal>
      </Container>
    )
  }
}

Widget.propTypes = {
  widgets: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  bizlogics: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.bool
  ]),
  widgetlibs: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.bool
  ]),
  onLoadWidgets: PropTypes.func,
  onLoadBizlogics: PropTypes.func,
  onLoadWidgetlibs: PropTypes.func,
  onDeleteWidget: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  widgets: makeSelectWidgets(),
  bizlogics: makeSelectBizlogics(),
  widgetlibs: makeSelectWidgetlibs()
})

export function mapDispatchToProps (dispatch) {
  return {
    onLoadWidgets: () => promiseDispatcher(dispatch, loadWidgets),
    onLoadBizlogics: () => promiseDispatcher(dispatch, loadBizlogics),
    onLoadWidgetlibs: () => promiseDispatcher(dispatch, loadWidgetlibs),
    onDeleteWidget: (id) => () => promiseDispatcher(dispatch, deleteWidget, id)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Widget)
