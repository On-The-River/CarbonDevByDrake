<template>
    <div>
        <el-dialog :title="title" :visible.sync="dialogFormVisible" width="720px">
            <el-form label-position="left" label-width="130px" :model="form">
                <el-form-item label="出售数量(tCO2e)" prop="tradeQuantity">
                    <span class="require">*</span>
                    <el-input v-model="form.tradeQuantity" size="medium" style="width: 268px; top: -5px" />
                </el-form-item>
                <el-form-item label="出售单价(¥)" prop="negotiatedPrice">
                    <el-input v-model="form.assetUnitPrice" size="medium" style="width: 268px; top: -5px" />
                </el-form-item>
                <el-form-item label="出售截止时间" prop="expirationDate">
                    <el-date-picker type="date" placeholder="选择日期" size="medium" v-model="form.expirationDate"
                        value-format="yyyy-MM-dd HH:mm:ss" style="width: 268px; top: -5px" />
                </el-form-item>
                <el-form-item label="期望交割时间" prop="deliveryTime">
                    <el-date-picker type="date" placeholder="选择日期" size="medium" v-model="form.deliveryTime"
                        value-format="yyyy-MM-dd HH:mm:ss" style="width: 268px; top: -5px" />
                </el-form-item>
                <el-form-item label="期望交割方式" prop="deliveryMethod">
                    <el-select v-model="form.deliveryMethod" placeholder="协议转入、竞价交易、定价交易" size="medium"
                        style="width: 536px; top: -5px">
                        <el-option v-for="(item, index) in tradeMethods" :key="index" :label="item.name"
                            :value="item.value" />
                    </el-select>
                </el-form-item>
                <el-form-item label="期望交割场所" prop="deliveryExchange">
                    <el-select v-model="form.deliveryExchange" placeholder="全国碳排放权交易中心、北京环境交易所、上海环境能源交易所" size="medium"
                        style="width: 536px; top: -5px">
                        <el-option v-for="(item, index) in exchangeList" :key="index" :label="item.name"
                            :value="item.value" />
                    </el-select>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="submit('form')" class="light-green-btn">
                    确定
                </el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
export default {
    name: "carbonAddQuote",
    props: {
        list:[],
        dialogFormVisible:false,
        isCredit:false,
        
    },
    data()
    {
        return {
            dialogFormVisible: false,
            title: "添加报价",
            form: {
                tradeQuantity: "",
                assetUnitPrice: "",
                expirationDate: "",
                deliveryTime: "",
                deliveryMethod: "",
                deliveryExchange: ""
            },
            exchangeList: [],
            tradeMethods: []
        };
    },

    methods: {
        openDialog()
        {
            this.dialogFormVisible = true;
        },
        submit(formName)
        {
            this.$refs[formName].validate((valid) =>
            {
                if (valid)
                {
                    console.log("submit!");
                    this.dialogFormVisible = false;
                }
                else
                {
                    console.log("error submit!!");
                    return false;
                }
            });
        }
    },
    mounted()
    {
        this.exchangeList = this.$store.getters.dictExchange;
        this.tradeMethods = this.$store.getters.dictDeliveryMethod;
    },
    created(){},
}
</script>

<style scoped>

</style>