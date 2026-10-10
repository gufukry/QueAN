<script setup lang="ts">
/** 通用二次确认模态框（删除等危险操作） */
defineProps<{
  open: boolean
  title: string
  message: string
  confirmText?: string
  busy?: boolean
  error?: string
  /** 可选勾选项（如「彻底删除」），勾选状态由父组件持有 */
  option?: string
  checked?: boolean
}>()

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
  (e: 'update:checked', value: boolean): void
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-mask" @pointerdown.self="!busy && emit('cancel')">
      <div class="modal">
        <h3 class="modal-title">{{ title }}</h3>
        <p class="confirm-text">{{ message }}</p>
        <p v-if="error" class="confirm-error" role="alert">{{ error }}</p>
        <label v-if="option" class="confirm-option">
          <input
            type="checkbox"
            :checked="checked === true"
            :disabled="busy"
            @change="emit('update:checked', ($event.target as HTMLInputElement).checked)"
          />
          <span>{{ option }}</span>
        </label>
        <div class="modal-actions">
          <button class="btn btn-ghost" :disabled="busy" @click="emit('cancel')">取消</button>
          <button class="btn btn-danger" :disabled="busy" @click="emit('confirm')">
            {{ busy ? '处理中…' : (confirmText ?? '确认删除') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.confirm-option {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 12px 0 4px;
  font-size: var(--text-sm);
  line-height: 1.5;
  cursor: pointer;
}
.confirm-option input {
  margin-top: 3px;
  flex-shrink: 0;
}
.confirm-text {
  font-size: var(--text-md);
  line-height: 1.7;
  word-break: break-all;
}
.confirm-error { color:var(--danger);font-size:var(--text-sm);line-height:1.7;white-space:pre-wrap;overflow-wrap:anywhere;margin:12px 0; }
</style>
