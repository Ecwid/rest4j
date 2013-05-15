/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rest4j.impl;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class JSONEscapingFilter extends FilterReader {
	char[] ring = new char[4096];
	int start, end;
	int prev;
	final static char[] hexChars = new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

	void push(char c) {
		if (end == (start+ring.length-1) % ring.length) {
			// extend the buffer
			char[] newRing = new char[ring.length*2];
			int i = 0, cc;
			while ((cc = pop()) != -1) {
				newRing[i++] = (char)cc;
			}
			ring = newRing;
			start = 0;
			end = i;
		}
		ring[end] = c;
		end = (end + 1) % ring.length;
	}

	int pop() {
		if (start == end) return -1;
		int c = ring[start];
		start = (start + 1) % ring.length;
		return c;
	}

	/**
	 * Creates a new filtered reader.
	 *
	 * @param in a Reader object providing the underlying stream.
	 * @throws NullPointerException if <code>in</code> is <code>null</code>
	 */
	public JSONEscapingFilter(Reader in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int c = pop();
		if (c != -1) {
			prev = c;
			return c;
		}

		c = super.read();
		return update(c);
	}

	private int update(int c) {
		switch (c) {
			case -1: return -1;
			case '\\':
			case '"':
				push((char)c);
				return '\\';
			case '/':
				if (prev == '<') {
					push((char)c);
					return '\\';
				}
				prev = c;
				return c;
			case '\b':
				push('b');
				return '\\';
			case '\t':
				push('t');
				return '\\';
			case '\n':
				push('n');
				return '\\';
			case '\f':
				push('f');
				return '\\';
			case '\r':
				push('r');
				return '\\';
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
						(c >= '\u2000' && c < '\u2100')) {
					push('u');
					for (int i=12; i>=0; i-=4) {
						push(hexChars[(c >> i) & 15]);
					}
					return '\\';
				} else {
					prev = c;
					return c;
				}
		}
	}

	void escapeAndPush(int c) {
		switch (c) {
			case '\\':
			case '"':
				push('\\');
				push((char)c);
				break;
			case '/':
				if (prev == '<') {
					push('\\');
					push((char)c);
					break;
				}
				prev = c;
				push((char)c);
				break;
			case '\b':
				push('\\');
				push('b');
				break;
			case '\t':
				push('\\');
				push('t');
				break;
			case '\n':
				push('\\');
				push('n');
				break;
			case '\f':
				push('\\');
				push('f');
				break;
			case '\r':
				push('\\');
				push('r');
				break;
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
						(c >= '\u2000' && c < '\u2100')) {
					push('\\');
					push('u');
					for (int i=12; i>=0; i-=4) {
						push(hexChars[(c >> i) & 15]);
					}
				} else {
					prev = c;
					push((char)c);
				}
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int c, originalLen = len;
		while (len>0 && (c = pop()) != -1) {
			cbuf[off++] = (char)c;
			len --;
		}
		if (len > 0) {
			char[] buf1 = new char[len];
			int len1 = super.read(buf1, 0, len);
			if (len1 == -1) {
				if (originalLen == len) return -1;
				return originalLen - len;
			}
			int i=0;
			for (; i<len1 && len>0; i++) {
				cbuf[off++] = (char)update(buf1[i]);
				len --;
				while (len>0 && (c = pop()) != -1) {
					cbuf[off++] = (char)c;
					len --;
				}
			}
			for (; i<len1; i++) {
				escapeAndPush(buf1[i]);
			}
		}
		if (originalLen == len) return -1;
		return originalLen - len;
	}

	@Override
	public long skip(long n) throws IOException {
		for (long i=0; i<n; i++) {
			if (read() == -1) return i;
		}
		return n;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public boolean ready() throws IOException {
		return start != end || super.ready();
	}
}
