import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request, setAuthToken, setUnauthorizedHandler } from './client';
import { jsonResponse } from '../test/renderWithProviders';

describe('request', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    setAuthToken(null);
  });

  it('sends the bearer token and JSON content type', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ ok: true }));
    setAuthToken('abc');

    await request('/local-pokemon', { method: 'POST', body: '{}' });

    const [url, init] = fetchMock.mock.calls[0];
    const headers = init!.headers as Headers;
    expect(url).toBe('/api/local-pokemon');
    expect(headers.get('Authorization')).toBe('Bearer abc');
    expect(headers.get('Content-Type')).toBe('application/json');
  });

  it('maps the backend error contract to ApiError', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse({ status: 400, message: 'Invalid', fieldErrors: { region: 'bad' } }, 400),
    );

    const error = (await request('/x').catch((e: unknown) => e)) as ApiError;

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(400);
    expect(error.fieldErrors).toEqual({ region: 'bad' });
  });

  it('notifies when an authenticated request is rejected with 401', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ message: 'expired' }, 401));
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    setAuthToken('expired-token');

    await expect(request('/local-pokemon')).rejects.toBeInstanceOf(ApiError);
    expect(handler).toHaveBeenCalledOnce();
  });

  it('reports network failures with a friendly message', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));

    await expect(request('/x')).rejects.toMatchObject({ status: 0 });
  });

  it('returns undefined for 204 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 204 }));

    await expect(request('/x', { method: 'DELETE' })).resolves.toBeUndefined();
  });
});
