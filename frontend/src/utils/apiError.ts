import { isAxiosError } from 'axios';

export function getApiErrorMessage(
  error: unknown,
  fallback = 'Произошла неизвестная ошибка'
): string {
  if (isAxiosError(error)) {
    const data = error.response?.data;

    if (typeof data === 'string' && data.trim()) {
      return data;
    }

    if (data && typeof data === 'object') {
      const record = data as Record<string, unknown>;
      if (typeof record.error === 'string' && record.error.trim()) {
        return record.error;
      }
      if (typeof record.message === 'string' && record.message.trim()) {
        return record.message;
      }
    }

    const status = error.response?.status;
    if (status === 500) {
      return 'Внутренняя ошибка сервера. Проверьте заполнение полей и повторите попытку.';
    }
    if (status === 400) {
      return 'Некорректные данные. Проверьте форму и попробуйте снова.';
    }
    if (status === 404) {
      return 'Запрашиваемый ресурс не найден.';
    }
    if (status === 401 || status === 403) {
      return 'Недостаточно прав для выполнения операции.';
    }
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
}
