export interface Comment {
    id: number;
    author: {
      name: string;
      timeAgo: string;
    };
    content: string;
    code?: string;
  }
  
  export interface Question {
    id: number;
    author: {
      name: string;
      timeAgo: string;
    };
    title: string;
    content: string;
    tags: string[];
    stats: {
      views: number;
      comments: number;
      upvotes: number;
    };
    comments: Comment[];
}