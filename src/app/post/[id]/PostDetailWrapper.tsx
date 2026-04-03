'use client';

import dynamic from 'next/dynamic';
import {Post, Category} from '@/types';

const PostDetail = dynamic(() => import('@/components/post/PostDetail'), {ssr: false});

interface Props {
    post: Post;
    categories: Category[];
    relatedPosts: Post[];
    isAdmin: boolean;
}

export default function PostDetailWrapper({post, categories, relatedPosts, isAdmin}: Props) {
    return <PostDetail post={post} categories={categories} relatedPosts={relatedPosts} isAdmin={isAdmin}/>;
}
