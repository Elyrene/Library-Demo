type HttpMethod = 'GET' | 'POST' | 'DELET' | 'PUT' | 'PATCH';

interface HttpClientOption {
    baseURL?: string;
    headers?: HeadersInit;
    timeout?: number;
}

interface HttpError<T = unknown> {
    status: number;
    message: string;
    data: T;
}

interface Interceptors {
    request: (config: RequestInit) => RequestInit;
    response: <T>(response: T) => T;  
}

export class HttpClient {
    public baseURL: string;
    private defaultHeaders: HeadersInit;
    private timeout: number;
    public interceptors: Interceptors = {
        request: (config) => config,
        response: (Response) => Response, 
    }

    constructor({
        baseURL = '',
        headers = {},
        timeout = 10000,
    }:HttpClientOption = {}) {
        this.baseURL = baseURL;
        this.defaultHeaders = headers;
        this.timeout = timeout;
        this.interceptors = {
            request: (config) => config,
            response: (response) => response,
        }
    }

    private mergeHeaders(customHeaders?: HeadersInit): HeadersInit {
        return {
            ...(typeof this.defaultHeaders === 'object' && Array.isArray(this.defaultHeaders)
            ? {...this.defaultHeaders}
            : this.defaultHeaders),
            ...(typeof customHeaders === 'object' && Array.isArray(customHeaders)
            ? {...customHeaders}
            : customHeaders),
        }
    }

    async request<T = unknown>(
        method: HttpMethod,
        url: string,
        data: BodyInit | Record<string, unknown> | null = null,
        customConfig: Omit<RequestInit,  'method' | 'body' | 'signal'> & {headers?: HeadersInit}
    ):Promise<T> {
        const fullURL = this.baseURL + url;
        const controller = new AbortController();
        const timeoutId = setTimeout(() => {
            controller.abort;
        }, this.timeout);

        const config: RequestInit = {
            method,
            headers: this.mergeHeaders(customConfig.headers),
            signal: controller.signal,
            ...customConfig,
        }

        if (data && ['POST', 'PUT', 'PATCH'].includes(method)) {
            if (data instanceof FormData) {
                config.body = data;
                const headers = config.headers as Record<string, string>;
                delete headers['Content-Type'];
            } else {
                config.headers = {
                    ...(config.headers as Record<string, string>),
                    'Content-Type': 'application/json',
                }
                config.body = JSON.stringify(data);
            }
        }

        if (data && ['POST', 'PUT', 'PATCH'].includes(method)) {
            if (data instanceof FormData) {
                config.body = data;
                const headers = config.headers as Record<string, string>;
                delete headers['Content-Type'];
            } else {
                config.headers = {
                    ...(config.headers as Record<string, string>),
                    'Content-Type': 'application/json',
                }
                config.body = JSON.stringify(data);
            }
        }

        const interceptedConfig = this.interceptors.request(config);

        try {
            const response = await fetch(fullURL, interceptedConfig);
            clearTimeout(timeoutId);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                const error: HttpError = {
                    status:response.status,
                    message: (errorData as {message?: string}).message ?? response.statusText,
                    data: errorData,
                };
                throw error;
            }

            const result = (await response.json()) as T;
            return this.interceptors.response(result);
        } catch (error: unknown) {
            clearTimeout(timeoutId);
            if (error instanceof Error && error.name === 'AbortError') {
                throw new Error(`Request timed out after ${this.timeout} ms`);
            }
            throw error;
        }

    }

    get<T = unknown>(
        url: string,
        param: Record<string, string | number | boolean | string[] | number[]> = {},
        config: Omit<RequestInit, 'method'> = {}
    ):Promise<T> {
        const queryString = new URLSearchParams(
            Object.fromEntries(
                Object.entries(param).map(([k, v]) => [k, String(v)])
            )
        ).toString();
        const fullURL = queryString ? `${url}?${queryString}` : url;
        return this.request<T>('GET', fullURL, null, config);
    }

    post<T = unknown>(
        url: string,
        data: BodyInit | Record<string, unknown>,
        config: Omit<RequestInit, 'method' | 'body'> = {}
    ): Promise<T> {
        return this.request<T>('POST', url, data, config);
    }

    put<T = unknown>(
        url: string,
        data: BodyInit | Record<string, unknown>,
        config: Omit<RequestInit, 'method' | 'body'> = {}
    ): Promise<T> {
        return this.request<T>('PUT', url, data, config);
    }

    delete<T = unknown>(
        url: string,
        config: Omit<RequestInit, 'method' | 'body'> = {}
    ): Promise<T> {
        return this.request<T>('PUT', url, null, config);
    }

    setRequestInterceptor(interceptor: Interceptors['request']): void {
        this.interceptors.request = interceptor;
    }

    setResponseInterceptor(interceptor: Interceptors['response']): void {
        this.interceptors.response = interceptor;
    }
}