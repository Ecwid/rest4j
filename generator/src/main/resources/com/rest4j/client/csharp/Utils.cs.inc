using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;

#region Copyright
/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
#endregion

namespace <xsl:value-of select="$namespace"/>
<![CDATA[{
    class JsonUtil
    {
        internal static decimal? toDecimal(object value)
        {
            if (value is long) return (decimal?)(long)value;
            if (value is int) return (decimal?)(int)value;
            if (value is float) return (decimal?)(float)value;
            if (value is double) return (decimal?)(double)value;
            return (decimal?)value;
        }

        internal static bool? toBoolean(object value)
        {
            if (value == null) return null;
            if (value is Boolean) return (bool)value;
            if (value is Decimal) return (decimal)value != 0;
            if (value is int) return (int)value != 0;
            if (value is long) return (long)value != 0;
            if (value is string) return "true".Equals(value);
            throw new ArgumentException("Not a boolean: " + value);
        }

        internal static DateTime? toDate(object value)
        {
            if (value == null) return null;
            if (value is Decimal) return new DateTime((int)value * 1000L);
            if (value is int) return new DateTime((int)value * 1000L);
            if (value is long) return new DateTime((long)value * 1000L);
            if (value is string) return DateTime.Parse((string)value);
            throw new ArgumentException("Not a date: " + value);
        }

        internal static JArray asJsonArray<T>(
            IList<T> list, Convert<JToken, T> write)
        {
            if (list == null) return null;
            if (list is ConvertingList<T>)
            {
                return ((ConvertingList<T>)list).originalList;
            }
            var result = new JArray();
            for (int i = 0; i < list.Count; i++)
            {
                var converted = write(list[i]);
                result.Add(converted);
            }
            return result;
        }

        internal static JObject asJsonMap<T>(
            IDictionary<string, T> dict, Convert<JToken, T> write)
        {
            if (dict == null) return null;
            if (dict is ConvertingDictionary<T>)
            {
                return ((ConvertingDictionary<T>)dict).originalDict;
            }
            var result = new JObject();
            foreach (var pair in dict)
            {
                var converted = write(pair.Value);
                result.Add(pair.Key, converted);
            }
            return result;
        }

    }

    public delegate T Convert<T, F>(F val);

    class ConvertingEnumerator<T, F> : IEnumerator<T>
    {
        private readonly IEnumerator<F> peer;
        private readonly Convert<T, F> read;
        private IEnumerator<F> enumerator;

        public ConvertingEnumerator(IEnumerator<F> peer, Convert<T, F> convert)
        {
            this.peer = peer;
            this.read = convert;
        }

        T IEnumerator<T>.Current
        {
            get { return read(peer.Current); }
        }

        void IDisposable.Dispose()
        {
            peer.Dispose();
        }

        object System.Collections.IEnumerator.Current
        {
            get { return read(peer.Current); }
        }

        bool System.Collections.IEnumerator.MoveNext()
        {
            return peer.MoveNext();
        }

        void System.Collections.IEnumerator.Reset()
        {
            peer.Reset();
        }

    }


    class ConvertingList<T> : IList<T>
    {
        private readonly JArray _list;
        private readonly Convert<T, JToken> read;
        private readonly Convert<JToken, T> write;

        public ConvertingList(JArray list, Convert<T, JToken> read, Convert<JToken, T> write)
        {
            this._list = list;
            this.read = read;
            this.write = write;
        }

        public JArray originalList
        {
            get { return _list; }
        }

        #region Implementation of IEnumerable

        public IEnumerator<T> GetEnumerator()
        {
            return new ConvertingEnumerator<T, JToken>(_list.GetEnumerator(), read);
        }

        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }

        #endregion

        #region Implementation of ICollection<T>

        public void Add(T item)
        {
            _list.Add(write(item));
        }

        public void Clear()
        {
            _list.Clear();
        }

        public bool Contains(T item)
        {
            throw new NotImplementedException();
        }
        public void CopyTo(T[] array, int arrayIndex)
        {
            JToken[] array1 = new JToken[array.Length];
            _list.CopyTo(array1, arrayIndex);
            for (int i = 0; i < array.Length; i++)
            {
                array[i] = read(array1[i]);
            }
        }

        public bool Remove(T item)
        {
            throw new NotImplementedException();
        }

        public int Count
        {
            get { return _list.Count; }
        }

        public bool IsReadOnly
        {
            get { return _list.IsReadOnly; }
        }

        #endregion

        #region Implementation of IList<T>

        public int IndexOf(T item)
        {
            throw new NotImplementedException();
        }

        public void Insert(int index, T item)
        {
            _list.Insert(index, write(item));
        }

        public void RemoveAt(int index)
        {
            _list.RemoveAt(index);
        }

        public T this[int index]
        {
            get { return read(_list[index]); }
            set { _list[index] = write(value); }
        }

        #endregion
    }

    class ConvertingCollection<T,F>: ICollection<T>
    {
        private readonly ICollection<F> _collection;
        private readonly Convert<T, F> read;
        private readonly Convert<F, T> write;

        public ConvertingCollection(ICollection<F> collection, Convert<T, F> read, Convert<F, T> write)
        {
            this._collection = collection;
            this.read = read;
            this.write = write;
        }

        #region Implementation of IEnumerable

        public IEnumerator<T> GetEnumerator()
        {
            return new ConvertingEnumerator<T, F>(_collection.GetEnumerator(), read);
        }

        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }

        #endregion

        public void Add(T item)
        {
            _collection.Add(write(item));
        }

        public void Clear()
        {
            _collection.Clear();
        }

        public bool Contains(T item)
        {
            throw new NotImplementedException();
        }
        public void CopyTo(T[] array, int arrayIndex)
        {
            F[] array1 = new F[array.Length];
            _collection.CopyTo(array1, arrayIndex);
            for (int i = 0; i < array.Length; i++)
            {
                array[i] = read(array1[i]);
            }
        }

        public bool Remove(T item)
        {
            throw new NotImplementedException();
        }

        public int Count
        {
            get { return _collection.Count; }
        }

        public bool IsReadOnly
        {
            get { return _collection.IsReadOnly; }
        }
    }

    class ConvertingDictionary<T> : ConvertingCollection<KeyValuePair<string,T>,KeyValuePair<string,JToken>>,  IDictionary<string, T>
    {
        private readonly JObject _dict;
        private readonly Convert<T, JToken> read;
        private readonly Convert<JToken, T> write;

        public ConvertingDictionary(JObject dict, Convert<T, JToken> read, Convert<JToken, T> write)
            : base(dict,
                    keyPair => new KeyValuePair<string, T>(keyPair.Key, read(keyPair.Value)),
                    keyPair => new KeyValuePair<string, JToken>(keyPair.Key, write(keyPair.Value))
            )
        {
            this._dict = dict;
            this.read = read;
            this.write = write;
        }

        public JObject originalDict
        {
            get
            {
                return _dict;
            }
        }

        public void Add(string key, T value)
        {
            _dict.Add(key, write(value));
        }

        public bool ContainsKey(string key)
        {
            JToken val;
            return _dict.TryGetValue(key, out val);
        }

        public ICollection<string> Keys
        {
            get
            {
                var list = new List<string>();
                foreach (JProperty prop in _dict.Properties()) {
                    list.Add(prop.Name);
                }
                return list;
            }
        }

        public bool Remove(string key)
        {
            return _dict.Remove(key);
        }

        public bool TryGetValue(string key, out T value)
        {
            JToken val;
            if (_dict.TryGetValue(key, out val)) {
                value = read(val);
                return true;
            }
            value = default(T);
            return false;
        }

        public ICollection<T> Values
        {
            get {
                var list = new List<JToken>();
                foreach (JProperty prop in _dict.Properties())
                {
                    list.Add(prop.Value);
                }
                return new ConvertingCollection<T, JToken>(list, read, write);
            }
        }

        public T this[string key]
        {
            get
            {
                return read(_dict[key]);
            }
            set
            {
                _dict[key] = write(value);
            }
        }

    }
}
]]>