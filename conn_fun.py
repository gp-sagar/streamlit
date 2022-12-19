import streamlit as st
import pandas as pd
import mysql.connector

# Database connection
@st.experimental_singleton
def init_connection():
    return mysql.connector.connect(**st.secrets["mysql"])
conn = init_connection()

# Fetchone Data
@st.experimental_memo(ttl=600)
def run_query(query):
    with conn.cursor(dictionary=True) as cur:
        cur.execute(query)
        return pd.DataFrame.from_records(cur.fetchall())

# Download function
def download(data_frame, file_name):
    @st.cache
    def df_to_csv(df):
        return df.to_csv().encode('utf-8')
    csv = df_to_csv(data_frame)

    st.download_button(
        label='Download Data',
        data=csv,
        file_name='{0}.csv'.format(file_name),
        mime='text/csv',
    )

# Query to dataFrame
@st.experimental_memo(ttl=600)
def query_to_df(query):
    with conn.cursor(dictionary=True) as cur:
        cur.execute(query)
        return pd.DataFrame.from_records(cur.fetchall())