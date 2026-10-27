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

interface bookInfo {
    id: number;
    name: string;
    author: string;
    publish: string;
    isbn: string;
    introduction: string;
    language: string;
    price: number;
    publishDate: string;
    typeId: number;
    status: number;
}


interface lendInfo {
    id: number;
    bookId: number;
    readerId: number;
    lendDate: string;
    backDate?: string | null;
    backType?: number | null;
    exceptRemarks?: string | null;
    bookName?: string | null;
    bookAuthor?: string | null;
    readerRealName?: string | null;
    readerNumber?: string | null;
}

interface noticeInfo {
    id: number;
    topic: string;
    content: string;
    author?: string | null;
    createDate?: string | null;
}