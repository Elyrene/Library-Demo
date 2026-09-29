interface readerInfo {
    id: number;
    username: string;
    realName: string;
    sex: string;
    birthday: string;
    address: string;
    tel: string;
    email: string;
    registerDate: string;
    readerNumber: string;
    cookie?: string;
}

interface adminInfo {
    id: number;
    username: string;
    adminType: number;
    cookie?: string;
}

interface SessionData {
    user: readerInfo | adminInfo | null;
    role: string;
    cookie: string;
}