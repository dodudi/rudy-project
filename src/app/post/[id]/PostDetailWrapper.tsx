'use client';

import dynamic from 'next/dynamic';
import {Post, Category} from '@/types';

const PostDetail = dynamic(() => import('@/components/post/PostDetail'), {ssr: false});

interface Props {
    post: Post;
    categories: Category[];
    isAdmin: boolean;
}

export default function PostDetailWrapper({post, categories, isAdmin}: Props) {
    return <PostDetail post={post} categories={categories} isAdmin={isAdmin}/>;
}
