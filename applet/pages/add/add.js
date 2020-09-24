// pages/add/add.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    inputShowed: false,
    inputVal: "",
    categoryArray: ['西瓜红', '济薯26', '烟薯25'],
    categoryValue: 0,
    sizeArray: ['大', '中', '小'],
    sizeValue: 0,
    orderList: [{
      userName: '',
      userPhone: '',
      createTime: '',
      orders: [{
        title: ''
      }]
    }]
  },
  // 打开搜索input
  showInput: function () {
    this.setData({
      inputShowed: true
    });
  },
  // 禁用搜索input
  hideInput: function () {
    this.setData({
      inputVal: "",
      inputShowed: false
    });
  },
  // 清空搜索框内容
  clearInput: function () {
    this.setData({
      inputVal: ""
    });
  },
  // 当输入框输入的时候触发的操作
  inputTyping: function (e) {
    console.log(e)
    this.setData({
      inputVal: e.detail.value
    });
  },
  testAdd(e) {
    console.log(e.currentTarget.dataset.name)
  },
  // 选择框
  categoryPickerChange: function (e) {
    this.setData({
      categoryValue: e.detail.value
    })
  },
  // 选择框
  sizePickerChange: function (e) {
    this.setData({
      sizeValue: e.detail.value
    })
  },
})