import { defineStore } from 'pinia';

export type NotifyKind = 'success' | 'error' | 'warning' | 'info';

export interface ToastItem {
  id: number;
  kind: NotifyKind;
  title: string;
  message?: string;
}

export interface ModalState {
  visible: boolean;
  mode: 'alert' | 'confirm';
  kind: NotifyKind;
  title: string;
  message: string;
  details?: string;
}

let toastSeq = 0;

export const useNotifyStore = defineStore('notify', {
  state: () => ({
    toasts: [] as ToastItem[],
    modal: {
      visible: false,
      mode: 'alert' as const,
      kind: 'error' as NotifyKind,
      title: '',
      message: '',
      details: undefined
    } satisfies ModalState,
    modalResolver: null as ((value: boolean) => void) | null
  }),
  actions: {
    pushToast(kind: NotifyKind, title: string, message?: string, durationMs = 4500) {
      const id = ++toastSeq;
      this.toasts.push({ id, kind, title, message });
      window.setTimeout(() => this.removeToast(id), durationMs);
    },
    removeToast(id: number) {
      this.toasts = this.toasts.filter((item) => item.id !== id);
    },
    success(title: string, message?: string) {
      this.pushToast('success', title, message);
    },
    warning(title: string, message?: string) {
      this.pushToast('warning', title, message);
    },
    info(title: string, message?: string) {
      this.pushToast('info', title, message);
    },
    error(title: string, message?: string, details?: string) {
      this.modal = {
        visible: true,
        mode: 'alert',
        kind: 'error',
        title,
        message: message || 'Попробуйте ещё раз или обратитесь к администратору.',
        details
      };
      this.modalResolver = null;
    },
    confirm(title: string, message: string): Promise<boolean> {
      return new Promise((resolve) => {
        this.modal = {
          visible: true,
          mode: 'confirm',
          kind: 'warning',
          title,
          message
        };
        this.modalResolver = resolve;
      });
    },
    closeModal(confirmed = false) {
      this.modal.visible = false;
      if (this.modalResolver) {
        this.modalResolver(confirmed);
        this.modalResolver = null;
      }
    }
  }
});
