interface AuthLoginRes {
    code: number;
    mes: string;
}

interface AuthMeRes {
    code: number;
    mes: string;
    data: {
        role: string;
        user: readerInfo | adminInfo;
    }
}

interface AuthLogoutRes {
    code: number;
    mes: string;
}

interface ReadersRegisterRes {
    code: number;
    mes: string;
}

// GET /api/readers/{id} 响应
interface ReadersIdRes {
    code: number;
    mes: string;
    data: readerInfo;
}
