<template>
  <Teleport to="body">
    <div class="toast-stack" aria-live="polite">
      <TransitionGroup name="toast">
        <article
          v-for="toast in notify.toasts"
          :key="toast.id"
          :class="['toast', `toast-${toast.kind}`]"
        >
          <div class="toast-icon">{{ iconFor(toast.kind) }}</div>
          <div class="toast-body">
            <strong>{{ toast.title }}</strong>
            <p v-if="toast.message">{{ toast.message }}</p>
          </div>
          <button type="button" class="toast-close" @click="notify.removeToast(toast.id)" aria-label="Закрыть">
            ×
          </button>
        </article>
      </TransitionGroup>
    </div>

    <Transition name="modal">
      <div v-if="notify.modal.visible" class="modal-overlay" @click.self="onOverlayClick">
        <div class="modal-card" role="dialog" aria-modal="true">
          <div :class="['modal-icon', `modal-icon-${notify.modal.kind}`]">
            {{ iconFor(notify.modal.kind) }}
          </div>
          <h2 class="modal-title">{{ notify.modal.title }}</h2>
          <p class="modal-message">{{ notify.modal.message }}</p>
          <pre v-if="notify.modal.details" class="modal-details">{{ notify.modal.details }}</pre>

          <div class="modal-actions">
            <button
              v-if="notify.modal.mode === 'confirm'"
              type="button"
              class="btn btn-ghost"
              @click="notify.closeModal(false)"
            >
              Отмена
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="notify.closeModal(notify.modal.mode === 'confirm')"
            >
              {{ notify.modal.mode === 'confirm' ? 'Подтвердить' : 'Понятно' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { useNotifyStore, type NotifyKind } from '../stores/notify';

const notify = useNotifyStore();

function iconFor(kind: NotifyKind) {
  if (kind === 'success') return '✓';
  if (kind === 'warning') return '!';
  if (kind === 'info') return 'i';
  return '×';
}

function onOverlayClick() {
  if (notify.modal.mode === 'alert') {
    notify.closeModal(false);
  }
}
</script>

<style scoped>
.toast-stack {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: min(420px, calc(100vw - 32px));
}

.toast {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: start;
  padding: 16px 18px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
  border: 1px solid #e2e8f0;
}

.toast-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1rem;
}

.toast-success .toast-icon { background: #dcfce7; color: #166534; }
.toast-error .toast-icon { background: #fee2e2; color: #991b1b; }
.toast-warning .toast-icon { background: #fef3c7; color: #92400e; }
.toast-info .toast-icon { background: #dbeafe; color: #1d4ed8; }

.toast-body strong {
  display: block;
  color: #0f172a;
  margin-bottom: 4px;
}

.toast-body p {
  margin: 0;
  color: #64748b;
  font-size: 0.92rem;
  line-height: 1.4;
}

.toast-close {
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 1.4rem;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.modal-card {
  width: min(480px, 100%);
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.2);
  text-align: center;
}

.modal-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 800;
}

.modal-icon-error { background: #fee2e2; color: #dc2626; }
.modal-icon-warning { background: #fef3c7; color: #d97706; }
.modal-icon-success { background: #dcfce7; color: #16a34a; }
.modal-icon-info { background: #dbeafe; color: #2563eb; }

.modal-title {
  margin: 0 0 10px;
  font-size: 1.25rem;
  color: #0f172a;
}

.modal-message {
  margin: 0;
  color: #475569;
  line-height: 1.5;
}

.modal-details {
  margin: 16px 0 0;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #334155;
  font-size: 0.85rem;
  text-align: left;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 160px;
  overflow: auto;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.btn {
  padding: 12px 22px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
}

.btn-ghost {
  background: #fff;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(24px);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition: transform 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-card,
.modal-leave-to .modal-card {
  transform: scale(0.96) translateY(8px);
}
</style>
