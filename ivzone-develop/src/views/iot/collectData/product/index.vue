<template>
  <UView name="产品数据">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <USelectItem field="productId" label="所属产品" labelField="name" valueField="id"
                     url="/iot/product/listByNonGateway" @loaded="productLoaded" @change="productChange"/>
        <UInputItem field="deviceSn" label="设备编号"/>
        <UButton func="query" url="/iot/collectData/product" ref="searchRef">搜索</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1100}">
<!--      <template #status="{record, text}">-->
<!--        <a-tag color="#87d068" v-if="text">成功</a-tag>-->
<!--        <a-tag color="#f50" v-else>失败</a-tag>-->
<!--      </template>-->
<!--          <template #action="{record}">-->
<!--            <UFuncTag func="del" :data="record" url="/iot/device/del">删除</UFuncTag>-->
<!--          </template>-->
    </UViewTable>
  </UView>
</template>
<!-- 设备功能 -->
<script>
import {ref} from "vue";
import CoreConsts from "@/components/CoreConsts";
export default {
  name: "ProductData",
  setup() {
    let columns = ref([
      {field: "deviceName", title: "设备名称", width: 120},
      {field: "deviceSn", title: "设备编号", width: 120},
      {field: "collectTime", title: "采集时间", width: 100, type: 'date', format: 'YYYY-MM-DD HH:mm:ss', fixed: 'right'},
    ]);

    // 只显示点位采集的数据
    let searchModel = ref({collectMode: 'model'});
    return {columns, searchModel}
  },
  beforeRouteEnter(to, form, next) {
    next(vm => {
      if(to.query.taskId) {
        vm.searchModel['collectTaskId'] = to.query.taskId;
      }
    })
  },
  methods: {
    selectParent(selectedKeys) {
      this.searchModel.deviceTypeId = selectedKeys[0]
      this.$refs['searchRef'].trigger();
    },
    columnFormatter({column, record}) {
      let value = record.value, result;
      if(value) {
        let split = value.split(':::');
        if(split && split.length > 0) {
          for(let item of split) {
            if(item.startsWith(column.field)) {
              result = item.substring(column.field.length + 1);
              break
            }
          }
        }
      }

      return result;
    },
    loadModelAttr(productId) {
      this.$http.get(`/iot/modelAttr/list?productId=${productId}`).then(({code, message, data}) => {
        if(code === CoreConsts.SuccessCode) {
          if(data instanceof Array) {
            let columns = data.filter(item => item.dataType !== 'json' && item.attrType != 'W').map(item => {
              let formatter = this.columnFormatter;
              let title = item.unit ? `${item.name}(${item.unit})` : item.name;
              return {field: item.field, title, width: 100, unit: item.unit, formatter}
            })

            this.columns = [
              {field: "deviceName", title: "设备名称", width: 120},
              {field: "deviceSn", title: "设备编号", width: 120}
              , ...columns,
              {field: "collectTime", title: "采集时间", width: 100, type: 'date', format: 'YYYY-MM-DD HH:mm:ss', fixed: 'right'}
            ]
          }
          this.$refs['searchRef'].trigger();
        } else {
          this.$msg.error(message);
        }
      })
    },
    productLoaded(list) {
      if(typeof list == 'object') {
        this.searchModel['productId'] = Object.values(list)[0].id;
        this.loadModelAttr(this.searchModel.productId);
      }
    },
    productChange(value) {
      this.searchModel['productId'] = value;
      this.loadModelAttr(this.searchModel.productId);
    },
  }
}
</script>
<style scoped> </style>
